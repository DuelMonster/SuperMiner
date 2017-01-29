package duelmonster.superminer.util;

import java.util.Iterator;

import com.google.common.collect.AbstractIterator;

import net.minecraft.entity.Entity;
import net.minecraft.util.Vec3;

public class BlockPos extends Vec3i {
	/** The BlockPos with all coordinates 0 */
	public static final BlockPos ORIGIN = new BlockPos(0, 0, 0);
	
	private static final int NUM_X_BITS() {
		return 1 + calculateLogBaseTwo(roundUpToPowerOfTwo(30000000));
	}
	
	private static final int NUM_Z_BITS() {
		return NUM_X_BITS();
	}
	
	private static final int NUM_Y_BITS() {
		return 64 - NUM_X_BITS() - NUM_Z_BITS();
	}
	
	private static final int Y_SHIFT() {
		return 0 + NUM_Z_BITS();
	}
	
	private static final int X_SHIFT() {
		return Y_SHIFT() + NUM_Y_BITS();
	}
	
	private static final long X_MASK() {
		return (1L << NUM_X_BITS()) - 1L;
	}
	
	private static final long Y_MASK() {
		return (1L << NUM_Y_BITS()) - 1L;
	}
	
	private static final long Z_MASK() {
		return (1L << NUM_Z_BITS()) - 1L;
	}
	
	/** A table of sin values computed from 0 (inclusive) to 2*pi (exclusive), with steps of 2*PI / 65536. */
	private static float[]		SIN_TABLE	= new float[65536];
	private static final int[]	multiplyDeBruijnBitPosition;
	static {
		for (int var0 = 0; var0 < 65536; ++var0)
			SIN_TABLE[var0] = (float) Math.sin(var0 * Math.PI * 2.0D / 65536.0D);
		
		multiplyDeBruijnBitPosition = new int[] { 0, 1, 28, 2, 29, 14, 24, 3, 30, 22, 20, 15, 25, 17, 4, 8, 31, 27, 13, 23, 21, 19, 16, 7, 26, 12, 18, 6, 11, 5, 10, 9 };
	}
	
	private static int calculateLogBaseTwo(int p_151239_0_) {
		/**
		 * Uses a B(2, 5) De Bruijn sequence and a lookup table to efficiently calculate the log-base-two of the given
		 * value. Optimized for cases where the input value is a power-of-two. If the input value is not a power-of-
		 * two, then subtract 1 from the return value.
		 */
		return calculateLogBaseTwoDeBruijn(p_151239_0_) - (isPowerOfTwo(p_151239_0_) ? 0 : 1);
	}
	
	private static int calculateLogBaseTwoDeBruijn(int p_151241_0_) {
		p_151241_0_ = isPowerOfTwo(p_151241_0_) ? p_151241_0_ : roundUpToPowerOfTwo(p_151241_0_);
		return multiplyDeBruijnBitPosition[(int) (p_151241_0_ * 125613361L >> 27) & 31];
	}
	
	private static boolean isPowerOfTwo(int p_151235_0_) {
		return p_151235_0_ != 0 && (p_151235_0_ & p_151235_0_ - 1) == 0;
	}
	
	private static int roundUpToPowerOfTwo(int p_151236_0_) {
		int j = p_151236_0_ - 1;
		j |= j >> 1;
		j |= j >> 2;
		j |= j >> 4;
		j |= j >> 8;
		j |= j >> 16;
		return j + 1;
	}
	
	public BlockPos(int x, int y, int z) {
		super(x, y, z);
	}
	
	public BlockPos(double x, double y, double z) {
		super(x, y, z);
	}
	
	public BlockPos(Entity source) {
		this(source.posX, source.posY, source.posZ);
	}
	
	public BlockPos(Vec3 source) {
		this(source.xCoord, source.yCoord, source.zCoord);
	}
	
	public BlockPos(Vec3i source) {
		this(source.getX(), source.getY(), source.getZ());
	}
	
	/**
	 * Add the given coordinates to the coordinates of this BlockPos
	 */
	public BlockPos add(double x, double y, double z) {
		return x == 0.0D && y == 0.0D && z == 0.0D ? this : new BlockPos(this.getX() + x, this.getY() + y, this.getZ() + z);
	}
	
	/**
	 * Add the given coordinates to the coordinates of this BlockPos
	 */
	public BlockPos add(int x, int y, int z) {
		return x == 0 && y == 0 && z == 0 ? this : new BlockPos(this.getX() + x, this.getY() + y, this.getZ() + z);
	}
	
	/**
	 * Add the given Vector to this BlockPos
	 */
	public BlockPos add(Vec3i vec) {
		return vec.getX() == 0 && vec.getY() == 0 && vec.getZ() == 0 ? this : new BlockPos(this.getX() + vec.getX(), this.getY() + vec.getY(), this.getZ() + vec.getZ());
	}
	
	/**
	 * Subtract the given Vector from this BlockPos
	 */
	public BlockPos subtract(Vec3i vec) {
		return vec.getX() == 0 && vec.getY() == 0 && vec.getZ() == 0 ? this : new BlockPos(this.getX() - vec.getX(), this.getY() - vec.getY(), this.getZ() - vec.getZ());
	}
	
	/**
	 * Offset this BlockPos 1 block up
	 */
	public BlockPos up() {
		return this.up(1);
	}
	
	/**
	 * Offset this BlockPos n blocks up
	 */
	public BlockPos up(int n) {
		return this.offset(EnumFacing.UP, n);
	}
	
	/**
	 * Offset this BlockPos 1 block down
	 */
	public BlockPos down() {
		return this.down(1);
	}
	
	/**
	 * Offset this BlockPos n blocks down
	 */
	public BlockPos down(int n) {
		return this.offset(EnumFacing.DOWN, n);
	}
	
	/**
	 * Offset this BlockPos 1 block in northern direction
	 */
	public BlockPos north() {
		return this.north(1);
	}
	
	/**
	 * Offset this BlockPos n blocks in northern direction
	 */
	public BlockPos north(int n) {
		return this.offset(EnumFacing.NORTH, n);
	}
	
	/**
	 * Offset this BlockPos 1 block in southern direction
	 */
	public BlockPos south() {
		return this.south(1);
	}
	
	/**
	 * Offset this BlockPos n blocks in southern direction
	 */
	public BlockPos south(int n) {
		return this.offset(EnumFacing.SOUTH, n);
	}
	
	/**
	 * Offset this BlockPos 1 block in western direction
	 */
	public BlockPos west() {
		return this.west(1);
	}
	
	/**
	 * Offset this BlockPos n blocks in western direction
	 */
	public BlockPos west(int n) {
		return this.offset(EnumFacing.WEST, n);
	}
	
	/**
	 * Offset this BlockPos 1 block in eastern direction
	 */
	public BlockPos east() {
		return this.east(1);
	}
	
	/**
	 * Offset this BlockPos n blocks in eastern direction
	 */
	public BlockPos east(int n) {
		return this.offset(EnumFacing.EAST, n);
	}
	
	/**
	 * Offset this BlockPos 1 block in the given direction
	 */
	public BlockPos offset(EnumFacing facing) {
		return this.offset(facing, 1);
	}
	
	/**
	 * Offsets this BlockPos n blocks in the given direction
	 */
	public BlockPos offset(EnumFacing facing, int n) {
		return n == 0 ? this : new BlockPos(this.getX() + facing.getFrontOffsetX() * n, this.getY() + facing.getFrontOffsetY() * n, this.getZ() + facing.getFrontOffsetZ() * n);
	}
	
	/**
	 * Calculate the cross product of this and the given Vector
	 */
	@Override
	public BlockPos crossProduct(Vec3i vec) {
		return new BlockPos(this.getY() * vec.getZ() - this.getZ() * vec.getY(), this.getZ() * vec.getX() - this.getX() * vec.getZ(), this.getX() * vec.getY() - this.getY() * vec.getX());
	}
	
	/**
	 * Serialize this BlockPos into a long value
	 */
	public long toLong() {
		return (this.getX() & X_MASK()) << X_SHIFT() | (this.getY() & Y_MASK()) << Y_SHIFT() | (this.getZ() & Z_MASK()) << 0;
	}
	
	/**
	 * Create a BlockPos from a serialized long value (created by toLong)
	 */
	public static BlockPos fromLong(long serialized) {
		int i = (int) (serialized << 64 - X_SHIFT() - NUM_X_BITS() >> 64 - NUM_X_BITS());
		int j = (int) (serialized << 64 - Y_SHIFT() - NUM_Y_BITS() >> 64 - NUM_Y_BITS());
		int k = (int) (serialized << 64 - NUM_Z_BITS() >> 64 - NUM_Z_BITS());
		return new BlockPos(i, j, k);
	}
	
	public static Iterable<BlockPos> getAllInBox(BlockPos from, BlockPos to) {
		final BlockPos blockpos = new BlockPos(Math.min(from.getX(), to.getX()), Math.min(from.getY(), to.getY()), Math.min(from.getZ(), to.getZ()));
		final BlockPos blockpos1 = new BlockPos(Math.max(from.getX(), to.getX()), Math.max(from.getY(), to.getY()), Math.max(from.getZ(), to.getZ()));
		return new Iterable<BlockPos>() {
			@Override
			public Iterator<BlockPos> iterator() {
				return new AbstractIterator<BlockPos>() {
					private BlockPos lastReturned = null;
					
					@Override
					protected BlockPos computeNext() {
						if (this.lastReturned == null) {
							this.lastReturned = blockpos;
							return this.lastReturned;
						} else if (this.lastReturned.equals(blockpos1)) {
							return this.endOfData();
						} else {
							int i = this.lastReturned.getX();
							int j = this.lastReturned.getY();
							int k = this.lastReturned.getZ();
							
							if (i < blockpos1.getX()) {
								++i;
							} else if (j < blockpos1.getY()) {
								i = blockpos.getX();
								++j;
							} else if (k < blockpos1.getZ()) {
								i = blockpos.getX();
								j = blockpos.getY();
								++k;
							}
							
							this.lastReturned = new BlockPos(i, j, k);
							return this.lastReturned;
						}
					}
				};
			}
		};
	}
	
	public static Iterable<BlockPos.MutableBlockPos> getAllInBoxMutable(BlockPos from, BlockPos to) {
		final BlockPos blockpos = new BlockPos(Math.min(from.getX(), to.getX()), Math.min(from.getY(), to.getY()), Math.min(from.getZ(), to.getZ()));
		final BlockPos blockpos1 = new BlockPos(Math.max(from.getX(), to.getX()), Math.max(from.getY(), to.getY()), Math.max(from.getZ(), to.getZ()));
		return new Iterable<BlockPos.MutableBlockPos>() {
			@Override
			public Iterator<BlockPos.MutableBlockPos> iterator() {
				return new AbstractIterator<BlockPos.MutableBlockPos>() {
					private BlockPos.MutableBlockPos theBlockPos = null;
					
					@Override
					protected BlockPos.MutableBlockPos computeNext() {
						if (this.theBlockPos == null) {
							this.theBlockPos = new BlockPos.MutableBlockPos(blockpos.getX(), blockpos.getY(), blockpos.getZ());
							return this.theBlockPos;
						} else if (this.theBlockPos.equals(blockpos1)) {
							return this.endOfData();
						} else {
							int i = this.theBlockPos.getX();
							int j = this.theBlockPos.getY();
							int k = this.theBlockPos.getZ();
							
							if (i < blockpos1.getX()) {
								++i;
							} else if (j < blockpos1.getY()) {
								i = blockpos.getX();
								++j;
							} else if (k < blockpos1.getZ()) {
								i = blockpos.getX();
								j = blockpos.getY();
								++k;
							}
							
							this.theBlockPos.x = i;
							this.theBlockPos.y = j;
							this.theBlockPos.z = k;
							return this.theBlockPos;
						}
					}
				};
			}
		};
	}
	
	public static final class MutableBlockPos extends BlockPos {
		/** Mutable X Coordinate */
		private int	x;
		/** Mutable Y Coordinate */
		private int	y;
		/** Mutable Z Coordinate */
		private int	z;
		
		public MutableBlockPos() {
			this(0, 0, 0);
		}
		
		public MutableBlockPos(int x_, int y_, int z_) {
			super(0, 0, 0);
			this.x = x_;
			this.y = y_;
			this.z = z_;
		}
		
		/**
		 * Get the X coordinate
		 */
		@Override
		public int getX() {
			return this.x;
		}
		
		/**
		 * Get the Y coordinate
		 */
		@Override
		public int getY() {
			return this.y;
		}
		
		/**
		 * Get the Z coordinate
		 */
		@Override
		public int getZ() {
			return this.z;
		}
		
		/**
		 * Set the values
		 */
		public BlockPos.MutableBlockPos set(int xIn, int yIn, int zIn) {
			this.x = xIn;
			this.y = yIn;
			this.z = zIn;
			return this;
		}
	}
}